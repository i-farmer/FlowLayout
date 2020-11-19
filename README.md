# FlowLayout
代码逻辑更简洁，支持设置左右对齐方式、行上下对齐方式，Item横向间距、竖向间距；同时支持 LayoutDirection.RTL

# 使用
```
1. add it in your root build.gradle at the end of repositories:
    allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
2. add the dependency
    dependencies {
	        implementation 'com.github.i-farmer:FlowLayout:1.0.0'
	}
3. to use
    <i.farmer.flowlayout.FlowLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:flowGravity="right"
        app:rowGravity="center"
        app:spacingHorizontal="5dp"
        app:spacingVertical="5dp">
        ...
    </i.farmer.flowlayout.FlowLayout>
    
```
