package net.bmbsolutions;

public interface PossibleSolution<T, O> {
    long getScore();

    int getSize();


    void swap(Gene<T> geneA, Gene<T> geneB);

    Gene<T> getGene();
    Gene<T> getValidGene(int reference, PossibleSolution<T, O> solution, Gene<T> gene);

    O asOutput(GAParams params, int generation);

    PossibleSolution<T, O> copy();

    void crossover(int reference, Gene<T> gene);

    void saveGene(int i, Gene<T> gene);
}
