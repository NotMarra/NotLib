Base example of a NotListener for a NotPlugin

## MyListener.java
```java
public class MyListener extends NotListener {
    public static final String ID = "mylistener";

    public MyListener(NotPlugin plugin) { super(plugin); }

    @Override
    public String getId() { return ID; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // do something...
    }
}
```

## NotPlugin.java
```java
@Override
public void initNotPlugin() {
    addListener(new MyListener(this));
}
```