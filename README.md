# FlowLayout
代码逻辑更简洁，支持设置左右对齐方式、行上下对齐方式，Item横向间距、竖向间距；同时支持 LayoutDirection.RTL

# 使用

    <i.farmer.flowlayout.FlowLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:flowGravity="right"
        app:rowGravity="center"
        app:spacingHorizontal="5dp"
        app:spacingVertical="5dp">
        ...
    </i.farmer.flowlayout.FlowLayout>
