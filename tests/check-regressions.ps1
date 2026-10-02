# Run with PowerShell 7 and JAVA_HOME pointing to JDK 21+.
# Compile the real command classes against small Minecraft boundary stubs.
param([string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$work = Join-Path ([IO.Path]::GetTempPath()) ('easybot-check-' + [guid]::NewGuid())
$null = New-Item -ItemType Directory $work
$javac = Join-Path $JavaHome 'bin/javac.exe'
$java = Join-Path $JavaHome 'bin/java.exe'

$metadata = Get-Content "$repo/src/main/resources/META-INF/neoforge.mods.toml" -Raw
if ($metadata -notmatch '(?m)^\[\[mixins\]\]\s*\r?\nconfig = "easybot\.mixins\.json"') {
    throw 'NeoForge must register easybot.mixins.json in [[mixins]]'
}
$mixins = Get-Content "$repo/src/main/resources/easybot.mixins.json" -Raw | ConvertFrom-Json
if ('ServerStatsCounterAccessor' -notin $mixins.mixins) { throw 'Statistics accessor is not registered' }

$sources = @{
    'net/minecraft/network/chat/Component.java' = @'
package net.minecraft.network.chat;
public record Component(String text) {
    public static Component literal(String text) { return new Component(text); }
}
'@
    'net/minecraft/commands/CommandSource.java' = @'
package net.minecraft.commands;
import net.minecraft.network.chat.Component;
public interface CommandSource {
    void sendSystemMessage(Component c);
    boolean acceptsSuccess(); boolean acceptsFailure(); boolean shouldInformAdmins();
}
'@
    'net/minecraft/commands/CommandSourceStack.java' = @'
package net.minecraft.commands;
public class CommandSourceStack {
    public final CommandSource source;
    public CommandSourceStack(CommandSource source, Object... args) { this.source = source; }
}
'@
    'net/minecraft/world/phys/Vec2.java' = 'package net.minecraft.world.phys; public class Vec2 { public static final Vec2 ZERO = new Vec2(); }'
    'net/minecraft/world/phys/Vec3.java' = 'package net.minecraft.world.phys; public class Vec3 { public static final Vec3 ZERO = new Vec3(); }'
    'net/minecraft/server/permissions/PermissionSet.java' = 'package net.minecraft.server.permissions; public class PermissionSet { public static final Object ALL_PERMISSIONS = new Object(); }'
    'com/springwater/easybot/utils/TextUtils.java' = 'package com.springwater.easybot.utils; public class TextUtils { public static String toLegacyString(net.minecraft.network.chat.Component c) { return c.text(); } }'
    'com/springwater/easybot/config/ConfigLoader.java' = @'
package com.springwater.easybot.config;
public class ConfigLoader {
    public static int waitTime = 1;
    public static ConfigLoader get() { return new ConfigLoader(); }
    public ConfigLoader getCommand() { return this; }
    public int getWaitTime() { return waitTime; }
}
'@
    'com/springwater/easybot/platforms/EasyBotModImpl.java' = @'
package com.springwater.easybot.platforms;
import java.util.function.Consumer;
import net.minecraft.commands.*;
public class EasyBotModImpl {
    public static final EasyBotModImpl INSTANCE = new EasyBotModImpl();
    public Runnable pending;
    public Consumer<CommandSource> action;
    private boolean onMainThread;
    public EasyBotModImpl getServer() { return this; }
    public Object overworld() {
        if (!onMainThread) throw new AssertionError("World accessed outside main thread");
        return this;
    }
    public EasyBotModImpl getCommands() { return this; }
    public void execute(Runnable task) { pending = task; }
    public void flush() {
        onMainThread = true;
        try { pending.run(); } finally { onMainThread = false; }
    }
    public void performPrefixedCommand(CommandSourceStack stack, String command) {
        if (!onMainThread) throw new AssertionError("Command ran outside main thread");
        action.accept(stack.source);
    }
}
'@
    'RegressionCheck.java' = @'
import com.springwater.easybot.impl.*;
import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.platforms.EasyBotModImpl;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class RegressionCheck {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        var server = EasyBotModImpl.INSTANCE;
        var commands = new CommandImpl();
        var output = new AtomicReference<CommandSource>();
        for (boolean synchronousPrefix : new boolean[]{false, true}) {
            server.action = source -> {
                output.set(source);
                if (synchronousPrefix) source.sendSystemMessage(Component.literal("sync"));
            };
            var result = commands.DispatchCommandAsync("test");
            // Waiting in the server queue must not consume the collection window.
            Thread.sleep(1100);
            check(!result.isDone(), "Completed before command execution");
            server.flush();
            check(!result.isDone(), "Synchronous output ended collection");
            output.get().sendSystemMessage(Component.literal("first"));
            check(!result.isDone(), "First asynchronous message ended collection");
            Thread.sleep(100);
            output.get().sendSystemMessage(Component.literal("second"));
            String expected = synchronousPrefix ? "sync,first,second" : "first,second";
            check(expected.equals(result.get(3, TimeUnit.SECONDS)), "Lost command output");
            output.get().sendSystemMessage(Component.literal("late"));
            check(expected.equals(result.join()), "Late output changed completed result");
        }
        server.action = source -> {};
        checkEmpty(commands, server);
        for (int waitTime : new int[]{0, -1}) {
            ConfigLoader.waitTime = waitTime;
            checkEmpty(commands, server);
        }
        server.action = source -> { throw new IllegalArgumentException("failed"); };
        var failure = commands.DispatchCommandAsync("bad");
        server.flush();
        try { failure.get(1, TimeUnit.SECONDS); throw new AssertionError("Lost exception"); }
        catch (ExecutionException e) { check(e.getCause() instanceof IllegalArgumentException, "Wrong exception"); }

        var source = new CommandSourceImpl();
        source.sendSystemMessage(Component.literal("initial"));
        var snapshot = source.getMessages();
        var writers = new Thread[4];
        for (int i = 0; i < writers.length; i++) {
            writers[i] = new Thread(() -> {
                for (int n = 0; n < 1000; n++) source.sendSystemMessage(Component.literal("item"));
            });
            writers[i].start();
        }
        for (int n = 0; n < 100; n++) source.getMessages().forEach(c -> check(c != null, "Corrupt snapshot"));
        for (var writer : writers) writer.join();
        check(snapshot.size() == 1, "Snapshot was modified by later writes");
        check(source.getMessages().size() == 4001, "Concurrent writes lost messages");
        System.out.println("PASS: Mixin registration, async/mixed output, queue delay, timeout, errors and concurrent snapshots");
    }
    static void checkEmpty(CommandImpl commands, EasyBotModImpl server) throws Exception {
        var result = commands.DispatchCommandAsync("silent");
        server.flush();
        check(result.get(3, TimeUnit.SECONDS).isEmpty(), "Silent command must return empty output");
    }
}
'@
}
foreach ($entry in $sources.GetEnumerator()) {
    $path = Join-Path $work $entry.Key
    $null = New-Item -ItemType Directory -Force (Split-Path $path -Parent)
    [IO.File]::WriteAllText($path, $entry.Value)
}
$production = "$repo/src/main/java/com/springwater/easybot/impl"
$javaFiles = @(Get-ChildItem $work -Filter '*.java' -Recurse | ForEach-Object FullName)
& $javac -encoding UTF-8 -d "$work/classes" @javaFiles "$production/CommandImpl.java" "$production/CommandSourceImpl.java"
if ($LASTEXITCODE -ne 0) { throw 'Regression check compilation failed' }
& $java -cp "$work/classes" RegressionCheck
if ($LASTEXITCODE -ne 0) { throw 'Regression check failed' }
