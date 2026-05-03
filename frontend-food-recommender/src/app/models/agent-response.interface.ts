import { FoodItem } from './food-item.interface';

/**
 * Represents the response from the AI agent when processing a user message
 */
export interface AgentResponse {
  /**
   * The AI agent's text response to the user's message
   */
  response: string;

  /**
   * Array of food item recommendations based on the user's profile and query
   */
  recommendations: FoodItem[];
}

// Made with Bob
