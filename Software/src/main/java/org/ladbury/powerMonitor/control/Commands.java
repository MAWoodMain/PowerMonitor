package org.ladbury.powerMonitor.control;

public enum Commands
{
    get("Get"),
    set("Set");

    final String displayName;
    Commands(String displayName)
    {
        this.displayName=displayName;
    }
}
