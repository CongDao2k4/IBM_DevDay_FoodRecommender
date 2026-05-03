/**
 * Represents a food item with health and allergy information
 */
export interface FoodItem {
  /**
   * Unique identifier for the food item
   */
  id: string;

  /**
   * Name of the food item
   */
  name: string;

  /**
   * Category of the food (e.g., 'Vegetables', 'Fruits', 'Proteins', 'Dairy')
   */
  category: string;

  /**
   * Warning indicator flag - true if the food conflicts with user's allergies or medical conditions
   */
  warningIndicator: boolean;

  /**
   * Explanation from the AI agent about why this food is recommended or flagged
   */
  reason: string;
}

// Made with Bob
