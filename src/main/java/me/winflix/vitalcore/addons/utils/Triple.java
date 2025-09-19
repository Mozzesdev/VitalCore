package me.winflix.vitalcore.addons.utils;

public class Triple<A, B, C>
{
    private A first;
    private B second;
    private C third;
    
    public A getFirst() {
        return this.first;
    }
    
    public B getSecond() {
        return this.second;
    }
    
    public C getThird() {
        return this.third;
    }
    
    public void setFirst(final A first) {
        this.first = first;
    }
    
    public void setSecond(final B second) {
        this.second = second;
    }
    
    public void setThird(final C third) {
        this.third = third;
    }
    
    public Triple(final A first, final B second, final C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}