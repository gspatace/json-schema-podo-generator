package net.gspatace.json.schema.podo.generator.templating;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Single usage template that is executed only once
 * Requires the full final name, no extension separation
 *
 * @author George Spătăcean
 */
@AllArgsConstructor
@Getter
public class SupportFile {
    private final String templateName;
    private final String finalFileName;
}
