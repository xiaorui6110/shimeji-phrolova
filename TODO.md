# TODO：Phrolova 核心角色素材与配置

> Phrolova 是本项目的核心角色，项目名即取自于她。
> 当前 `img/Phrolova/` 仅为空目录，形象图像与专属动作/行为尚未实现，以下为后续补齐路线。

## 参照物

- 图像与动作布局参照现有角色，动作最全的可参考 `img/Eviling/`，经典款参考 `img/Shimeji/`
- 一个完整角色资产的标准结构：

```
img/<角色>/
├── conf/
│   ├── actions.xml      # 动作清单（ActionList，按 conf/Mascot.xsd 声明）
│   └── behaviors.xml    # 行为状态机（BehaviorList）
└── <动作帧>.png          # 按动作分帧的透明 PNG
```

- `actions.xml` / `behaviors.xml` 头部已引用 `Mascot.xsd` 做结构校验，XML 头部示例见现有角色

## 任务清单

- [ ] 1. 确定 Phrolova 的最终形象、尺寸与画布规格
      （ImageAnchor 等基准点参照现有角色，如 128 高的角色锚点取 64,128）
- [ ] 2. 绘制各动作帧 PNG（建议先覆盖核心动作，再逐步扩充）
      - 站立/坐下/躺下：Stand、Sit、LieDown 相关
      - 移动：Walk、Run、Dash、Creep（注意左右朝向帧）
      - 跌落与抓握：Fall、Dragged、Thrown、HoldOntoWall、HoldOntoCeiling
      - 交互/表情（可选）：Look、Tripping、Bouncing 等
      - 帧命名与姿态切分参照现有角色目录
- [ ] 3. 编写 `img/Phrolova/conf/actions.xml`
      - 至少包含运行时必需动作：Fall、Dragged、Thrown
      - 内置动作（Embedded，如 Look / Offset / Jump / Fall）直接引用现有内置类
      - 每个 Pose 标注 Image / ImageAnchor / Velocity / Duration
- [ ] 4. 编写 `img/Phrolova/conf/behaviors.xml`
      - 至少包含框架必需行为：ChaseMouse、Fall、Dragged、Thrown（现有角色这些项标有 ALWAYS REQUIRED 注释）
      - 再逐步加入空闲、探索、攀爬等自选行为状态机，并核对行为间引用一致性
- [ ] 5. 在 `conf/settings.properties` 的 `ActiveShimeji` 中追加 `Phrolova`，使角色进入可选列表
- [ ] 6. 本地验证
      - 两份 XML 通过 Mascot.xsd 校验（无 schema 报错）
      - `mvn -P run` 运行：角色正常显示、可拖拽/抛出、贴地/贴壁/吊顶与跌落行为正确、多显示器正常
- [ ] 7. 收尾
      - （可选）补充 Preview.png 预览图
      - 素材齐备后，同步移除 README 与文档站中"Phrolova 素材尚未完成"的说明

## 备注

- 行为表达式引用了未定义的脚本变量时会在运行时抛异常（日志出现 ReferenceError），新增/修改 XML 时请核对 `${...}`、`#{...}` 中引用的变量名
- 若角色存在多套换装/变体，参考 KuroShimeji 等目录的 conf/ 组织方式
