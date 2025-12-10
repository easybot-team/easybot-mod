package com.springwater.easybot.impl;

import com.springwater.easybot.EasyBotFabric;
import com.springwater.easybot.bridge.message.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class ComponentBuilderImpl {
    private static final Map<Class<? extends Segment>, BiConsumer<MutableComponent, Segment>> SEGMENT_HANDLER = Map.of(
            UnknownSegment.class, (c, s) -> handleUnknownSegment(c, (UnknownSegment) s),
            TextSegment.class, (c, s) -> handleTextSegment(c, (TextSegment) s),
            ImageSegment.class, (c, s) -> handleImageSegment(c, (ImageSegment) s),
            AtSegment.class, (c, s) -> handleAtSegment(c, (AtSegment) s),
            FileSegment.class, (c, s) -> handleFileSegment(c, (FileSegment) s),
            ReplySegment.class, (c, s) -> handleReplySegment(c, (ReplySegment) s),
            FaceSegment.class, (c, s) -> handleFaceSegment(c, (FaceSegment) s)
    );

    private static BiConsumer<MutableComponent, Segment> getHandler(Class<? extends Segment> classType) {
        // 如果 Map 中没有对应的 class，就返回 Unknown 的处理逻辑，或者直接返回 handleUnknownSegment 的通用包装
        return SEGMENT_HANDLER.getOrDefault(classType,
                (c, s) -> handleUnknownSegment(c, (UnknownSegment) s));
    }


    private static void handleUnknownSegment(MutableComponent component, UnknownSegment segment) {
        EasyBotFabric.LOGGER.error("[构建消息] 错误: 机器人收到未知字段 [type-{}", segment.getRawText());
        component.append(
                Component.literal("[未知字段]").withStyle(ChatFormatting.GRAY)
        );
    }

    private static void handleTextSegment(MutableComponent component, TextSegment segment) {
        component.append(
                Component.literal(segment.getText())
        );
    }

    private static void handleImageSegment(MutableComponent component, ImageSegment segment) {
        // TODO: 使用某种神秘方法配合可以将图片显示到聊天栏的插件
        Style style = Style.EMPTY;
        style = ComponentAdapterImpl.withHoverText(style, Component.literal("点我跳转").withStyle(ChatFormatting.GRAY));
        style = ComponentAdapterImpl.withOpenUrl(style, segment.getUrl());
        style = style.withColor(ChatFormatting.GREEN);
        component.append(
                Component.literal("[图片]")
                        .withStyle(style)
        );

    }

    private static void handleAtSegment(MutableComponent component, AtSegment segment) {
        // 这表示 这是一个@玩家的消息
        if (!Objects.equals(segment.getAtUserId(), "") && !Objects.equals(segment.getAtUserName(), "")) {
            Style style = Style.EMPTY;
            style = ComponentAdapterImpl.withHoverText(style, Component.empty()
                    .append(
                            Component.literal("玩家信息\n")
                    )
                    .append(
                            Component.literal("ID: " + segment.getAtUserId() + "\n")
                    )
                    .append(
                            Component.literal("名称: " + segment.getAtUserName()+ "\n")
                    )
                    .append(
                            Component.literal("全部绑定: " + String.join(",", segment.getAtPlayerNames()))
                    )
            );
            
            String atUserName = segment.getAtUserName();
            if(segment.getAtPlayerNames().length >= 1){
                atUserName = segment.getAtPlayerNames()[0];
            }
            
            component.append(
                    Component.empty()
                            .withStyle(style)
                            .append(
                                    Component.literal("@" + atUserName)
                                            .withStyle(style
                                                    .withColor(ChatFormatting.GOLD)
                                                    .withUnderlined(true)
                                                    .withBold(true)
                                            )
                            )
            );
        }

        if (Objects.equals(segment.getAtUserName(), "0")) {
            component.append(
                    Component.literal("@全体成员")
                            .withStyle(style -> style
                                    .withColor(ChatFormatting.GOLD)
                                    .withUnderlined(true)
                                    .withBold(true)
                            )
            );
        }

    }

    private static void handleFileSegment(MutableComponent component, FileSegment segment) {
        Style style = Style.EMPTY;
        style = ComponentAdapterImpl.withHoverText(style, Component.literal("点我下载: " + segment.getRawText()).withStyle(ChatFormatting.GRAY));
        style = ComponentAdapterImpl.withOpenUrl(style, segment.getFileUrl());
        style = style.withColor(ChatFormatting.GREEN);
        component.append(
                Component.literal("[文件]")
                        .withStyle(style)
        );
    }

    private static void handleReplySegment(MutableComponent component, ReplySegment segment) {
        Style style = Style.EMPTY;
        style = ComponentAdapterImpl.withHoverText(style, Component.literal("暂不支持查看消息 \nid=@" + segment.getId()).withStyle(ChatFormatting.GRAY));
        style = style.withColor(ChatFormatting.AQUA);
        component.append(
                Component.literal("[回复一条消息]")
                        .withStyle(style)
        );
    }

    private static void handleFaceSegment(MutableComponent component, FaceSegment segment) {
        Style style = Style.EMPTY;
        style = style.withColor(ChatFormatting.GREEN);
        component.append(
                Component.literal("[" + segment.getDisplayName() + "]")
                        .withStyle(style)
        );
    }

    public static MutableComponent build(List<Segment> segments) {
        MutableComponent component = Component.empty();
        for (Segment segment : segments) {
            var handler = getHandler(segment.getClass());
            if (handler != null) {
                try {
                    handler.accept(component, segment);
                } catch (ClassCastException e) {
                    EasyBotFabric.LOGGER.error("[构建消息] 错误: 段落类型不匹配 {}", e.getLocalizedMessage());
                    EasyBotFabric.LOGGER.error(e.toString());
                }
            }
        }
        return component;
    }
}