package net.gspatace.json.schema.podo.generator.core.services;

import lombok.extern.slf4j.Slf4j;
import net.gspatace.json.schema.podo.generator.core.annotations.CustomProperties;
import net.gspatace.json.schema.podo.generator.core.annotations.SchemaGenerator;
import net.gspatace.json.schema.podo.generator.core.base.AbstractGenerator;
import net.gspatace.json.schema.podo.generator.core.base.BaseOptions;
import org.reflections.Reflections;
import picocli.CommandLine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton that retrieves registered generators
 * and provides basic interaction with them, such as
 * <ul>
 *     <li> retrieval of registered generators</li>
 *     <li> instantiation of specific generator</li>
 *     <li> retrieval of generator specific custom options objects</li>
 *     <li> retrieval of details for all the specific options</li>
 * </ul>
 * <p>
 * Uses reflection
 *
 * @author George Spătăcean
 */
@Slf4j
public class GeneratorsService {
    private static final String GENERATOR_PACKAGE = "net.gspatace.json.schema.podo.generator.core.langs";
    private final Map<String, Class<?>> loadedGenerators = new ConcurrentHashMap<>();

    /**
     * Private constructor
     * Uses reflection to parse all generators annotated with {@link SchemaGenerator}
     * and populates internal list of generators to be used further
     */
    private GeneratorsService() {
        final Reflections reflections = new Reflections(GENERATOR_PACKAGE);
        final Set<Class<?>> generatorClasses = reflections.getTypesAnnotatedWith(SchemaGenerator.class);
        generatorClasses.forEach(clazz -> {
            final SchemaGenerator annotation = clazz.getAnnotation(SchemaGenerator.class);
            loadedGenerators.put(annotation.name(), clazz);
            log.trace("Found `{}` as a registered generator.", annotation.name());
        });
    }

    /**
     * Singleton accessor.
     *
     * @return a handler to the sole {@link GeneratorsService} instance.
     */
    public static GeneratorsService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Returns a list of description for each registered generator
     * by retrieving everything annotated with {@link SchemaGenerator}
     *
     * @return the list
     */
    public List<GeneratorDescription> getAvailableGenerators() {
        final List<GeneratorDescription> generators = new ArrayList<>();
        loadedGenerators.forEach((genName, genClazz) -> {
            final SchemaGenerator annotation = genClazz.getAnnotation(SchemaGenerator.class);
            final GeneratorDescription.GeneratorDescriptionBuilder builder = GeneratorDescription.builder();
            builder.name(annotation.name()).description(annotation.description());
            generators.add(builder.build());
            log.trace("Added generator description for `{}` generator", annotation.name());
        });
        return generators;
    }

    /**
     * Returns a Set of all the specific options of the given generator
     *
     * @param generatorName target generator for which options to be returned
     * @return Set containing all the options
     */
    public Set<OptionDescription> getSpecificGeneratorOptions(final String generatorName) {
        final Set<OptionDescription> options = new HashSet<>();
        final Optional<Object> generatorInstance = GeneratorsService.getInstance().getCustomOptionsCommand(generatorName);
        generatorInstance.ifPresent(theInstance -> {
            final CommandLine cmd = new CommandLine(theInstance);
            for (final CommandLine.Model.OptionSpec option : cmd.getCommandSpec().options()) {
                final OptionDescription optionDescription = OptionDescription.builder()
                        .name(String.join(";", option.names()))
                        .defaultValue(option.defaultValue())
                        .description(String.join(";", option.description()))
                        .build();
                options.add(optionDescription);
            }
        });

        return options;
    }

    /**
     * Instantiates a concrete generator that can be used
     * See cli commands.GenerateCommand#run()
     *
     * @param baseOptions generator options holder
     * @return the concrete generator
     */
    public Optional<AbstractGenerator> getGeneratorInstance(final BaseOptions baseOptions) {
        if (!loadedGenerators.containsKey(baseOptions.getGeneratorName())) {
            log.error("Generator `{}` was not found.", baseOptions.getGeneratorName());
            return Optional.empty();
        }

        final Class<?> clazz = loadedGenerators.get(baseOptions.getGeneratorName());
        try {
            final Constructor<?> constructor = clazz.getConstructor(BaseOptions.class);
            final AbstractGenerator generator = (AbstractGenerator) constructor.newInstance(baseOptions);
            return Optional.of(generator);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            log.error("Failed to instantiate generator `{}`:", baseOptions.getGeneratorName(), ex);
        }

        return Optional.empty();
    }

    /**
     * Retrieves an optional instance of a generator`s custom properties,
     * by searching a generator class for an inner class annotated with {@link CustomProperties}.
     * Used in cli commands.CustomConfigHelper#run()
     *
     * @param targetGenerator name of the generator
     * @return Optional instance of the custom properties
     */
    public Optional<Object> getCustomOptionsCommand(final String targetGenerator) {
        if (!loadedGenerators.containsKey(targetGenerator)) {
            log.error("Generator `{}` was not found.", targetGenerator);
            return Optional.empty();
        }

        final Class<?> generatorClazz = loadedGenerators.get(targetGenerator);
        try {
            final Class<?>[] declaredClasses = generatorClazz.getDeclaredClasses();
            final Optional<Class<?>> optionsClass = Arrays
                    .stream(declaredClasses)
                    .filter(clazz -> clazz.isAnnotationPresent(CustomProperties.class))
                    .findFirst();

            if (optionsClass.isPresent()) {
                final Constructor<?> constructor = optionsClass.get().getConstructor();
                return Optional.of(constructor.newInstance());
            }

            log.debug("CustomProperties class for generator `{}` not found", targetGenerator);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            log.error("Failed to instantiate CustomProperties class for generator `{}`", targetGenerator, ex);
        }
        return Optional.empty();
    }

    /**
     * Simple Singleton Holder of the parent class
     *
     * @author George Spătăcean
     */
    private static class SingletonHolder {
        private static final GeneratorsService INSTANCE = new GeneratorsService();
    }
}
