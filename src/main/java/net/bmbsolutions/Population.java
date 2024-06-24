package net.bmbsolutions;

public interface Population<T, O> {
    PossibleSolution<T, O> getFittestSolution();

    void saveSolution(int i, PossibleSolution<T, O> solution);

    PossibleSolution<T, O> getSolution(int index);

    void initialize();
}
