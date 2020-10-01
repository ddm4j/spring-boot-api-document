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
点击上面的 wiki，了解查看。
[link](https://github.com/ddm4j/spring-boot-api-document/wiki/spring-boot-api-document-使用指南)

# 版本历史
```
 2.4.0 ：ResponseCode 支持在配置文件中配置，修复已知BUG。
 2.3.1 ：修复错误的命名。
 2.3.0 : 修复已各BUG，优化文档显示，增加 ApiEnum 注解、用于描述 Enum的文档显示，增加 ApiResonseCode 注解、用于控制集中状态码，只在文档上该接口需要的状态码。 
 2.2.11：修复已知文档扫描BUG，修复 HttpServletXXX 对影响数据校验的BUG，参数忽略，增加 * 号匹配方式，只能用字段的前面或后面， @ApiResponseIgnore({"code*.xx*.*ABC"})。
 2.2.10：修复文档web在部分浏览器中不能正常显示问题，但依旧不支持 IE，优化注解中的注释。
 2.2.9 ：修复已知BUG。
 2.2.8 ：支持 java8 多重注解，ApiMethod,ApiController,ApiField 增加 name 属性，是 value 的别名。
 2.2.7 ：修复已知BUG，优化界面显示。
 2.2.6 ：修复上传文件(MultipartFile)校验异常。
 2.2.5 ：修复复杂 bean 在文档上显示异常问题，修复数组、集合bean 校验异常，调整接口文档内容过多显示问题。
 2.2.3 ：修复了接口上请求头参数，@RequestHeader 注解不能设置别名异常。
 2.2.2 ：修复静态类型字段，文档显示异常，关闭 全部校验功能后校验异常BUG，以及已知BUG
 2.2.1 ：发布新版本，增加可直接配置请求头，全部是否校验等功能，不兼容 1.X 版本。
 1.x.x ：测试版本，问题众多，功能较少。
```
