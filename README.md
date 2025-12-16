# EasyBot Mods

EasyBot的Minecraft模组实现端,基于[Stonecutter](https://stonecutter.kikugie.dev/wiki/start/)实现多个加载器、多个游戏版本适配。

## 使用说明

- 使用 Gradle 任务中的 `"Set active project to ..."` 来更新 `src/` 目录下类文件可用的 Minecraft 版本。
- 使用 `buildAndCollect` Gradle 任务将模组发布文件存储在 `build/libs/` 目录。

## 实用链接

- [Stonecutter 新手指南](https://stonecutter.kikugie.dev/wiki/start/)：*提示：您必须理解其运作原理！*
- [提问的智慧指南](https://github.com/ryanhanwu/How-To-Ask-Questions-The-Smart-Way/blob/main/README-zh_CN.md)：另附[视频版](https://www.youtube.com/results?search_query=How+To+Ask+Questions+The+Smart+Way)。


## 特别说明

不要使用IDE的全局代码优化功能,由于框架特性 部分“未使用”的代码，只会在实际打包时被引用，你应该选择性忽视他们 (如果你觉得烦可以加上if注释来消除这个警告,目前还没有做优化)