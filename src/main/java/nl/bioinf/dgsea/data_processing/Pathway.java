package nl.bioinf.dgsea.data_processing;
/**
 * Represents a biological pathway with a unique identifier and description.
 *
 * @param pathwayId The unique identifier for the pathway.
 * @param description A brief description of the pathway.
 */
public record Pathway(String pathwayId, String description) {
}
