# spring-boot-api-document
spring-boot-api-document 是一个基本SpringBoot开发的一个接口文档管理工具，清晰的向前端开发人员展示数据结构和数据标准，并具有后端数据校验功能。

# 技术要求
## 应用场景
1. **Springboot 项目，前后端分离项目**
2. **SpringCloud 项目，前后端分离项目**
3. **SpringCloudAlibaba 项目，前后端分离项目**

# 核心功能
 1. Api 接口文档管理。
 2. 后端数据校验

## 环境需求
 1. **springboot** : 2.x
 2. **jdk**:1.8

# 使用指南
[使用指南](https://github.com/ddm4j/spring-boot-api-document/wiki/spring-boot-api-document-使用指南)

# 版本历史
```
 2.7.3 : 1.修复无请求参数时，测试窗口异常。
 2.7.2 : 1.修复已知BUG。
 2.7.1 : 1.解决接口文档上，测试窗口，不能移动问题。
         2.接口 ApiParamCheckFailHandler，增加 返回值对象传入。
         3.接口 regexp 校验，支持使用 #{} 方式获取。
 2.7.0 : 1.增加 校验失败处理 接口 ApiParamCheckFailHandler，必须实现该接口，可用于校验失败后是否继续执行或返回错误信息，
         2.删除 ApiParam 注解 describe 属性。
         3.修复接口文档上测试窗口，uri 上需要携带参数，测试异常问题。 
 2.6.0 : 修复 list,set 校验，会出现空指针异常问题,ApiParam 注解增加 name 属性，describe 标识为过时属性。
 2.5.1 : 修改已知校验异常BUG，修改JSON请求带Get参数时，不能传参问题。
 2.5.0 : 接口文档页面上增加Api测试功能，Api测试功能不支持文件下载，增加对 @PatchMapper注解的支持。
 2.4.7 : 修复@ApiParamIgnore，会清除 @PathVariable 标识参数，并且将 @PathVariable 标识的参数单独在文档中显示，修复其它已知BUG。
 2.4.6 : 修复表单提交，数据为空异常
 2.4.5 : 修复已知BUG
 2.4.4 : 修复已知BUG，修复 @PathVariable 注解问题，请求类型错误问题，增加接口文档logo。
 2.4.3 : 修复已知BUG，单个 ApiParam 注解不能校验问题，接口文档页面，刷新需要重新输入账号密码问题。
 2.4.2 : 修复已知BUG。
 2.4.1 : 修复已知BUG。
 2.4.0 : ResponseCode 支持在配置文件中配置，修复已知BUG。
 2.3.1 : 修复错误的命名。
 2.3.0 : 修复已各BUG，优化文档显示，增加 ApiEnum 注解、用于描述 Enum的文档显示，增加 ApiResonseCode 注解、用于控制集中状态码，只在文档上该接口需要的状态码。 
 2.2.11: 修复已知文档扫描BUG，修复 HttpServletXXX 对影响数据校验的BUG，参数忽略，增加 * 号匹配方式，只能用字段的前面或后面， @ApiResponseIgnore({"code*.xx*.*ABC"})。
 2.2.10: 修复文档web在部分浏览器中不能正常显示问题，但依旧不支持 IE，优化注解中的注释。
 2.2.9 : 修复已知BUG。
 2.2.8 : 支持 java8 多重注解，ApiMethod,ApiController,ApiField 增加 name 属性，是 value 的别名。
 2.2.7 : 修复已知BUG，优化界面显示。
 2.2.6 : 修复上传文件(MultipartFile)校验异常。
 2.2.5 : 修复复杂 bean 在文档上显示异常问题，修复数组、集合bean 校验异常，调整接口文档内容过多显示问题。
 2.2.3 : 修复了接口上请求头参数，@RequestHeader 注解不能设置别名异常。
 2.2.2 : 修复静态类型字段，文档显示异常，关闭 全部校验功能后校验异常BUG，以及已知BUG
 2.2.1 : 发布新版本，增加可直接配置请求头，全部是否校验等功能，不兼容 1.X 版本。
 1.x.x : 测试版本，问题众多，功能较少。
```
