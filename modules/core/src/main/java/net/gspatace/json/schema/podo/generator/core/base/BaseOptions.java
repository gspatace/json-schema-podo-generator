package net.gspatace.json.schema.podo.generator.core.base;

import lombok.Builder;
import lombok.Data;

/**
 * Holder of properties that are to be passed to generators,
 * when generate command cli commands.GenerateCommand is invoked
 * Holds:
 * <ul>
 *  <li>generic options used by the generation framework</li>
 *  <li>array of specific generator properties that are passed to their specific generators</li>
 * </ul>
 *
 * @author George Spătăcean
 */
@Data
@Builder
public class BaseOptions {
    private final String generatorName;
    private final String inputSpec;
    private final String[] generatorSpecificProperties;
}
