## 介绍

__如果你对于写mapper文件非常厌恶，那么这个项目非常适合你__ 

__本项目依赖于mybatis及mybatis-plus, 并使引用了mybatis-plus中的一些代码__

本项目是为了减少mybatis中xml配置诞生的

本项目的主要的设计思想是借鉴spring data jpa的思路，通过方法名称自动推断sql，但比起spring data jpa，支持更为复杂的sql的表达

> 文档中的示例sql均为简化的sql，目的在于表达，并不是最终生成的sql
> 文档中的示例代码均为kotlin代码

## 主要特性

- 支持根据DAO的方法名称自动推断添加、查询、修改、删除、统计、是否存在等数据库操作
- 支持多种形式的表达,如findById,queryById,selectById是等价的，deleteById与removeById是等价的
- 支持根据对象结构自动解析resultMap（支持级联的对象），不再需要在xml文件中配置resultMap
- 支持join的推断，复杂的sql也能自动推断
- 支持分页操作
- 支持spring data的Pageable和Page对象，基本可以和jpa做到无缝切换
- 支持部分jpa注解：@Table、@Transient、@Id，作用于持久化对象

## 使用方法

第一步： 添加maven仓库
```
<distributionManagement>
    <repository>
      <id>nexus</id>
      <url>http://nexus.aegis-info.com/repository/maven-releases/</url>
    </repository>
</distributionManagement>
```

第二步：

在pom中引用依赖

```
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>${mybatis-spring-boot-starter.version}</version>
</dependency>
```

## 配置说明

本项目的引入使用无需任何配置（当然mybatis的配置是必要的）即可使用

@Mapper注解的DAO接口是否需要sql推断是__可选__的，且mapper的xml文件的配置是具有更高优先级的，如果一个方法在xml中存在配置，则sql推断自动失效

本插件的使用可以是渐进式的，一开始在项目中使用本插件对原项目没有任何影响，可以先尝试删除一些方法的xml配置，让其使用sql推断，如果能够正常工作，则可继续去除xml，直到xml达到最简化

## 启用sql推断

让@Mapper注解的DAO接口继承 __XmlLessMapper__ 接口即可实现DAO的sql推断

XmlLessMapper接口接收一个泛型参数，即该DAO要操作的对象，所有的sql推断都是基于该对象的

XmlLessMapper接口没有任何默认的方法，不会影响原有代码

原来使用mybatis-plus的方法注入需要继承BaseMapper接口，但BaseMapper接口有很多方法，可能大部分方法都是不需要的，所以我改写了这个逻辑，一个默认的方法也不添加，让开发自行添加DAO所需要的方法，

## 功能增强说明

表名称支持jpa注解__@Table__，原mybatis-plus的@TableName注解仍然有效，但@Table注解的优先级更高

主键属性支持jpa注解__@Id__

## sql推断说明

### select查询推断

- 从方法名称中推断的字段名称均为mapper关联数据对象的属性名称，而非数据库中的表字段名称

#### 例1 findById

解析为 
```sql
SELECT * FROM table WHERE id = #{id}
```

#### 例2 findByName

解析为 
```sql
SELECT * FROM table WHERE name = #{name}
```

#### 例3 findByNameLike

解析为
```sql
SELECT * FROM table WHERE name LIKE CONCAT('%',#{name}, '%')
```

#### 例4 findByNameLikeKeyword

解析为
```sql
SELECT * FROM table WHERE name LIKE CONCAT('%',#{keyword}, '%')
```

#### 例5 findByNameEqAndId 

解析为 

```sql
SELECT * FROM table WHERE name = #{name} AND id = #{id}
```

#### 例6 findIdAndNameByAge

解析为 
```sql
SELECT id, name FROM table WHERE age = #{age}
```

### sql推断名称与方法名称隔离

在mapper方法上使用@ResolvedName注解，该注解的必选参数name将会代替方法名称作为推断sql的名称，这样可以让方法名称更具语义化

例如 
```kotlin
@ResolvedName("findIdAndNameAndAge")
fun findSimpleInfoList(): List<User>
```
将使用 findIdAndNameAndAge 推断sql，推断的结果为：
```sql
SELECT id,name,age FROM user
```

### 指定方法获取的属性集合

使用 @SelectedProperties注解

例如
```kotlin
@SelectedProperties(properties=["id", "name", "age"])
fun findSimpleInfoList(): List<User>
```
 
上一个示例中的 @ResolvedName("findIdAndNameAndAge") 便可以用 @SelectedProperties(properties=["id", "name", "age"]) 来代替

- 注：使用@SelectedProperties注解之后，从方法名中推断的查询属性将被忽略


### delete操作推断

支持 deleteAll deleteById deleteByName的写法


### update操作推断

支持 update 一个对象或 update某个字段

为了防止出现数据更新错误，update操作必须指定对象的主键属性

例1：
```kotlin
fun update(user: User): Int
```

最终解析为：
```sql
UPDATE 
  user 
SET 
    user.name = #{name}, 
    user.password = #{password}, 
    user.email = #{email}
WHERE 
    id = #{id}
```


例2：
```kotlin
fun updateNameById(name:String,id:Int): Int
```

```sql
UPDATE 
  user 
SET 
    user.name = #{name} 
WHERE 
    id = #{id}
```

## 支持 Insert 操作

支持批量插入


## join的支持

### join 一个对象

在持久化对象中可以关联另外一个对象，这个对象对应数据库中的另外一张表，那么在查询的时候如果需要级联查询可以这样配置：

在关联的对象（支持单个对象或对象集合，即一对一或一对多的关系都可以支持）属性上添加注解：
```
 @JoinObject(
      targetTable = "t_score",
      targetColumn = "student_id",
      joinProperty = "id",
      associationPrefix = "score_",
      selectColumns = ["score", "subject_id"]
  )
```
注解中的属性作用如下：
targetTable 需要join的表
targetColumn join的表中用于关联的列名称
joinProperty 当前对象中用于关联的属性名称（注意是对象属性名称而不是列名称）
associationPrefix 为防止列名称冲突，给关联表的属性别名添加固定前缀
selectColumns 关联表中需要查询的列集合

- 注：如果关联的是对象集合，在kotlin中必须声明为可变的集合

