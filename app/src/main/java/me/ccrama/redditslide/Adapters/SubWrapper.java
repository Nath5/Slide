package me.ccrama.redditslide.Adapters;

public class SubWrapper {
    private String name;
    private boolean active = false;

    public SubWrapper( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive( boolean active ) {
        this.active = active;
    }
}
