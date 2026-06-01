# 酷安发布检查清单

## 已就绪
- [x] 正式签名 APK（videoplayer.keystore）
- [x] 应用截图方案（5 张，需真机截取）
- [x] 应用描述文案
- [x] 隐私政策页面（HTML）

## 待完成
- [ ] **真机截图**（1080×1920，PNG，不少于 3 张）
- [ ] **隐私政策托管**（上传到 GitHub Pages 并获取公开链接）
- [ ] **应用软著**（向国家版权局申请，约 1-2 个月）
- [ ] **酷安开发者认证**（登录 coolapk.com，完成实名认证）
- [ ] **包名优化**（建议改为 com.sotolish.videoplayer 等正式名称）
- [ ] **代码推送到 GitHub**（在本地运行 git push）

## 酷安提交流程

1. 登录 [developer.coolapk.com](https://developer.coolapk.com)
2. 完成实名认证
3. 创建新应用 → 填写信息（使用 coolapk/app_description.md 中的内容）
4. 上传 APK（app-release.apk，11MB）
5. 上传截图（>=3 张，1080×1920）
6. 填写隐私政策链接
7. 提交审核（通常 1-3 个工作日）

## 文件清单

| 文件 | 路径 |
|------|------|
| APK | app/build/outputs/apk/release/app-release.apk |
| 签名文件 | videoplayer.keystore |
| 描述文案 | coolapk/app_description.md |
| 隐私政策 | coolapk/privacy_policy.html |
