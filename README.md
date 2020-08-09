# spring-boot-api-document
spring-boot-api-document 是一个基本Springboot 开发的一个接口文档管理工具，清晰的向前端开发人员展示数据结构和数据标准，并具有后端数据校验功能。

# 技术要求
## 应用场景
1. **Springboot 项目，前后端分离项目**
2. **SpringCloud 项目，前后端分离项目**

# 核心功能
 1. Api 接口文档管理。
 2. 后端数据校验

## 环境需求
 1. **springboot** : 2.x
 2. **jdk**:1.8

# 使用指南
点击上面的 wiki，了解查看

# 版本历史
```
 2.2.8 ：支持 java8 多重注解，ApiMethod,ApiController,ApiField 增加 name 属性，是 value 的别名。
 2.2.7 : 修复已知BUG，优化界面显示。
 2.2.6 ：修复上传文件(MultipartFile)校验异常。
 2.2.5 ：修复复杂 bean 在文档上显示异常问题，修复数组、集合bean 校验异常，调整接口文档内容过多显示问题。
 2.2.3 ：修复了接口上请求头参数，@RequestHeader 注解不能设置别名异常。
 2.2.2 ：修复静态类型字段，文档显示异常，关闭 全部校验功能后校验异常BUG，以及已知BUG
 2.2.1 ：发布新版本，增加可直接配置请求头，全部是否校验等功能，不兼容 1.X 版本。
 1.x.x ：测试版本，问题众多，功能较少。
```
