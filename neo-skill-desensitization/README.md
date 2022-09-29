# 日志脱敏技术

## 序列化和反序列化

### 序列化

序列化就是把对象的状态信息转化为可存储或传输的字节序列形式过程，也就是把对象转化为字节序列的过程称为对象的序列化。

当把内存中的对象保存到一个文件中或者数据库中时候或者需要在网络上传输的时候，就需要用到序列化。

### 反序列化

序列化的逆向过程，把字节数组反序列化为对象，把字节序列恢复为对象的过程成为对象的反序列化。

![image-20220818145927964](https://raw.githubusercontent.com/gangster-bluelight/PictureBed/main/typora/202208181459029.png)

### 什么时候需要用到序列化和反序列化

当只在本地JVM里运行Java实例，这个时候不需要什么序列化和反序列化的；

当需要将内存中的对象持久化到磁盘，数据库中时，当需要与浏览器进行交互时，当需要实现RPC时, 这个时候就需要序列化和反序列化了。

前两个需要用到序列化和反序列化的场景，是不是有一个很大的疑问？在与浏览器交互时，还有将内存中的对象持久化到数据库中时，好像都没有去进行序列化和反序列化，因为没有实现Serializable接口，但一直正常运行。

> 结论：**只要对内存中的对象进行持久化或网络传输, 都需要序列化和反序列化.**

> 理由：服务器与浏览器交互时真的没有用到Serializable接口吗？JSON格式实际上就是将一个对象转化为字符串, 所以服务器与浏览器交互时的数据格式其实是字符串

```java
// String类型实现了Serializable接口, 并显示指定serialVersionUID的值.
public final class String implements java.io.Serializable, Comparable<String>, CharSequence {
    /** The value is used for character storage. */
    private final char value[];

    /** Cache the hash code for the string */
    private int hash; // Default to 0

    /** use serialVersionUID from JDK 1.0.2 for interoperability */
    private static final long serialVersionUID = -6849794470754667710L;

    ......
}
```

对象持久化到数据库中时，Mybatis数据库映射文件里的insert代码:

```xml
<!-- 实际上并不是将整个对象持久化到数据库中,而是将对象中的属性持久化到数据库中,而对象中的属性都是实现了Serializable接口的基本属性 -->
<insert id="insertUser" parameterType="org.tyshawn.bean.User">
    INSERT INTO t_user(name, age) VALUES (#{name}, #{age})
</insert>
```

### 实现序列化和反序列化为什么要实现Serializable接口

查看API文档时，就会发现Serializable接口是一个标记接口（没有成员方法和变量），在使用的时候只需要将要序列化的类实现Serializable接口

在Java中实现了Serializable接口后，JVM会在底层实现序列化和反序列化，如果不实现Serializable接口，那去写一套序列化和反序列化代码也行

使用Serializable接口实现序列化主要有两步：

1. 将要序列化的Person1类实现Serializable接口；
2. 经过ObjectOutputStream 的writeObject()方法把这个类的对象写到一个地方（文件），就完成了对象的序列化操作。

> 如是要一个类是可序列化的，那么子类即使不显示实现Serializable接口也是可序列化的

#### 为什么要显式指定serialVersionUID的值

如果不显式指定serialVersionUID，JVM在序列化时会根据属性自动生成一个serialVersionUID，然后与属性一起序列化，再进行持久化或网络传输。

如果没有显式指定serialVersionUID的值，在反序列化时，JVM会再根据属性自动生成一个新版serialVersionUID，然后将新版serialVersionUID与序列化时生成的旧版serialVersionUID进行比较，如果相同则反序列化成功，否则报错。

如果显示指定了serialVersionUID的值，JVM在序列化和反序列化时仍然都会生成一个serialVersionUID，值为显式指定的值，这样在反序列化时新旧版本的serialVersionUID就一致了.

> **在实际开发中，不显示指定serialVersionUID的情况会导致什么问题？**
>
> 如果类写完后不再修改，那当然不会有问题，但这在实际开发中是不可能的，类会不断迭代，一旦类被修改了，那旧对象反序列化就会报错。所以在实际开发中，都会显示指定一个serialVersionUID，值是多少无所谓，只要不变就行。

写个实例测试下:

(1) User类：不显示指定serialVersionUID.

```java
@Data
public class User implements Serializable {
    private String name;
    private Integer age;
}
```

(2) 测试类：先进行序列化, 再进行反序列化.

```java
public class SerializableTest {
    private static void serialize(User user) throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("D:\\111.txt")));
        oos.writeObject(user);
        oos.close();
    }

    private static User deserialize() throws Exception{
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("D:\\111.txt")));
        return (User) ois.readObject();
    }

    public static void main(String[] args) throws Exception {
        User user = new User();
        user.setName("tyshawn");
        user.setAge(18);
        System.out.println("序列化前的结果: " + user);

        serialize(user);

        User dUser = deserialize();
        System.out.println("反序列化后的结果: "+ dUser);
    }
}
```

(3) 结果：先注释掉反序列化代码, 执行序列化代码, 然后User类新增一个属性sex

```java
@Data
public class User implements Serializable {
    private String name;
    private Integer age;
    private String sex;
}
```

再注释掉序列化代码执行反序列化代码, 最后结果如下:

> 序列化前的结果：*【报错结果为序列化与反序列化产生的serialVersionUID不一致】*
>
> User{name='tyshawn', age=18} Exception in thread "main" java.io.InvalidClassException: org.tyshawn.SerializeAndDeserialize.User; local class incompatible: stream classdesc serialVersionUID = 1035612825366363028, local class serialVersionUID = -1830850955895931978

接下来在上面User类的基础上显示指定一个serialVersionUID

```
private static final long serialVersionUID = 1L;
```

再执行上述步骤, 测试结果如下:

> 序列化前的结果：*【显示指定serialVersionUID后就解决了序列化与反序列化产生的serialVersionUID不一致的问题】*
>
> User{name='tyshawn', age=18} 反序列化后的结果: User{name='tyshawn', age=18, sex='null'}

> **强烈建议在一个可序列化类中显示的定义serialVersionUID，为它赋予明确的值**

### Java序列化的其他特性

> 被transient关键字修饰的属性不会被序列化, static属性也不会被序列化.

#### transient修饰的属性

并不是所有java对象里的内容，都是想要序列化写出去的，例如一些隐私数据，对于上面的Person1对象，假如age是一个隐私字段，不想序列化写到磁盘上，可以用transient关键字来标记它

(1) User类

```java
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private Integer age;
    private transient String sex;
    private static String signature = "你眼中的世界就是你自己的样子";
}
```

(2) 测试类

```java
public class SerializableTest {
    private static void serialize(User user) throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("D:\\111.txt")));
        oos.writeObject(user);
        oos.close();
    }

    private static User deserialize() throws Exception{
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("D:\\111.txt")));
        return (User) ois.readObject();
    }

    public static void main(String[] args) throws Exception {
        User user = new User();
        user.setName("tyshawn");
        user.setAge(18);
        user.setSex("man");
        System.out.println("序列化前的结果: " + user);

        serialize(user);

        User dUser = deserialize();
        System.out.println("反序列化后的结果: "+ dUser);
    }
}
```

(3) 结果

先注释掉反序列化代码, 执行序列化代码, 然后修改User类signature = “我的眼里只有你”, 再注释掉序列化代码执行反序列化代码, 最后结果如下:

> 序列化前的结果: 
>
> User{name='tyshawn', age=18, sex='man', signature='你眼中的世界就是你自己的样子'} 反序列化后的结果: User{name='tyshawn', age=18, sex='null', signature='我的眼里只有你'}

#### static属性为什么不会被序列化

因为**序列化是针对对象而言**的，而static属性优先于对象存在，随着类的加载而加载，所以不会被序列化。

> 问题：serialVersionUID也被static修饰，为什么serialVersionUID会被序列化？
>
> 回答：其实serialVersionUID属性并没有被序列化，JVM在序列化对象时会自动生成一个serialVersionUID，然后将显示指定的serialVersionUID属性值赋给自动生成的serialVersionUID.