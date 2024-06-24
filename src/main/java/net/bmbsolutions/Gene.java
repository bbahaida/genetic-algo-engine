package net.bmbsolutions;

public interface Gene<T> {
    void setReference(int reference);

    void swap(Gene<T> gene);

    T getARN();

    Gene<T> copy();
}
