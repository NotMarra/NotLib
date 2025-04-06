Base example of a NotCommandGroup for a NotPlugin

## MyCommandGroup.java
```java
public final class MyCommandGroup extends NotCommandGroup {
    public static final String ID = "mycommandgroup";

    public MyCommandGroup(NotPlugin plugin) { super(plugin); }

    @Override
    public String getId() { return ID; }

    @Override
    public List<NotCommand> notCommands() {
        return List.of(
            testCommand()
        );
    }

    private NotCommand testCommand() {
        return NotCommand.of("testcommand", cmd -> {
            ChatF.of("Test command executed!").sendTo(cmd.getPlayer());
        });
    }
}
```

## NotPlugin.java
```java
@Override
public void initNotPlugin() {
    addCommandGroup(new MyCommandGroup(this));
}
```