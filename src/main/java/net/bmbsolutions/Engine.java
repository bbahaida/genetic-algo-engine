package net.bmbsolutions;

import java.util.LinkedList;
import java.util.stream.IntStream;

public abstract class Engine<T, I, O> {
    private static int score = 0;

    public O process(I input, GAParams params, EvolutionStrategy strategy) {
        PossibleSolution<T, O> fittestSolution = null;
        int generation = 0;
        double delta = 1000000000000L;
        LinkedList<Long> scores = new LinkedList<>();
        LinkedList<Long> deltas = new LinkedList<>();
        Population<T, O> population = getInstance(params.getPopulationSize(), input, params.isRandomize());
        population.initialize();
         while (!terminationCondition(generation, delta, params)) {

            ++generation;

            if (EvolutionStrategy.CROSSOVER.equals(strategy)) {
                population.initialize();
                population = evolvePopulationWithCrossover(input, params, population);
            } else {
                population = evolvePopulation(input, params);
            }

            PossibleSolution<T, O> solution = population.getFittestSolution();

            if (fittestSolution == null ||
                    solution.getScore() > fittestSolution.getScore()) {
                fittestSolution = solution.copy();
            }
            scores.add(fittestSolution.getScore());

            if (scores.size() > params.getDeltaStepper()) {
                scores.removeFirst();
            }

            long pv = 0;

            for (long s: scores) {
                long d = s - pv;
                pv = s;
                deltas.add(d);

                if (deltas.size() > params.getDeltaStepper() - 1) {
                    deltas.removeFirst();
                }
            }

            if (scores.size() >= params.getDeltaStepper()) {
                delta = deltas.stream().mapToDouble(Long::doubleValue).sum() - params.getDeltaConstant();
            }

            System.out.printf("____file: %s, gen: %d, score: %d, delta: %.2f _____%n",
                    params.getFilename(), generation, solution.getScore(), delta);

        }
        // find out the best distribution
        return fittestSolution.asOutput(params, generation);
    }

    private boolean terminationCondition(int generation, double delta, GAParams params) {
        return generation >= params.getMaxGeneration()
                || (params.getDeltaVariance() > 0 && delta < params.getDeltaVariance());
    }

    private Population<T, O> evolvePopulation(I input, GAParams params) {
        Population<T, O> newPopulation = getInstance(params.getPopulationSize(), input, params.isRandomize());
        newPopulation.initialize();

        for (int i = 0; i < params.getPopulationSize(); i++) {
            newPopulation.saveSolution(i, mutate(newPopulation.getSolution(i), params));
        }

        return newPopulation;
    }

    private Population<T, O> evolvePopulationWithCrossover(I input, GAParams params, Population<T, O> population) {
        Population<T, O> newPopulation = getInstance(params.getPopulationSize(), input, params.isRandomize());
        for (int i = 0; i < params.getPopulationSize(); i++) {
            PossibleSolution<T, O> firstIndividual = randomSelection(population, input, params);
            PossibleSolution<T, O> secondIndividual = randomSelection(population, input, params);

            PossibleSolution<T, O> newIndividual = crossover(firstIndividual, secondIndividual, params.getCrossoverRate());
            newPopulation.saveSolution(i, newIndividual);
        }

        for (int i = 0; i < params.getPopulationSize(); i++) {
            newPopulation.saveSolution(i, mutate(newPopulation.getSolution(i), params));
        }

        return newPopulation;
    }

    private PossibleSolution<T, O> mutate(PossibleSolution<T, O> solution,
                                          GAParams params) {
        for (int i = 0; i < solution.getSize(); i++) {
            if (Math.random() <= params.getMutationRate()) {
                IntStream.range(0, params.getMutationTotal())
                        .forEach(j -> solution.swap(solution.getGene(), solution.getGene()));
            }
        }
        return solution;
    }

    private PossibleSolution<T, O> crossover(PossibleSolution<T, O> firstIndividual, PossibleSolution<T, O> secondIndividual, double crossoverRate) {
        PossibleSolution<T, O> newSolution = firstIndividual.copy();
        int size = newSolution.getSize();
        for (int i = 0; i < size; i++) {
            if (Math.random() < crossoverRate) {
                Gene<T> geneA = firstIndividual.getGene();
                Gene<T> geneB = secondIndividual.getValidGene(i, newSolution, geneA);
                newSolution.crossover(i, geneB);
            } else {
                Gene<T> geneA = secondIndividual.getGene();
                Gene<T> geneB = firstIndividual.getValidGene(i, newSolution, geneA);
                newSolution.crossover(i, geneB);
            }
        }
        return newSolution;
    }

    private PossibleSolution<T, O> randomSelection(Population<T, O> population, I input, GAParams params) {
        Population<T, O> newPopulation = getInstance(params.getTournamentSize(), input, params.isRandomize());
        for (int i = 0; i < params.getTournamentSize(); i++) {
            int randomIndex = (int) (Math.random() * params.getTournamentSize());
            newPopulation.saveSolution(i, population.getSolution(randomIndex).copy());
        }
        return newPopulation.getFittestSolution();
    }

    public abstract Population<T, O> getInstance(int size, I input, boolean randomize);

    public O solve(GAParams params, Reader<I> reader, Writer<O> writer, EvolutionStrategy strategy) {
        System.out.printf("engine params: %s %n", params.toString());
        System.out.printf("reading file **** %s **** started%n", params.getFilename());
        String prefix = "src/main/resources/";
        String inputFilePath = prefix + params.getFilename() + ".in";
        // read the file
        I input = reader.read(inputFilePath);

        System.out.printf("reading file **** %s **** done%n", params.getFilename());

        // process the data
        System.out.printf("Start Processing %s %n", params.getFilename());
        O output = process(input, params, strategy);

        // write the data
        System.out.printf("writing file **** %s **** started%n", params.getFilename());
        String outputFilePath = prefix + params.getFilename() + ".out";
        writer.write(output, outputFilePath);
        System.out.printf("writing file **** %s **** done%n", params.getFilename());

        return output;
    }

}
