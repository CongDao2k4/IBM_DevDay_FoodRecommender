/**
 * Represents a user's health profile for personalized food recommendations
 */
export interface UserProfile {
  /**
   * Unique identifier for the user
   */
  id: string;

  /**
   * User's full name
   */
  name: string;

  /**
   * Body Mass Index (BMI) value
   */
  bmi: number;

  /**
   * List of medical conditions (e.g., 'Gout', 'Diabetes', 'Hypertension')
   */
  medicalConditions: string[];

  /**
   * List of allergens the user is allergic to (e.g., 'Seafood', 'Peanuts', 'Dairy')
   */
  allergens: string[];
}

// Made with Bob
