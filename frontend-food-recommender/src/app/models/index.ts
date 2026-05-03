/**
 * Barrel file for exporting all data models
 * This allows for clean imports throughout the application
 *
 * Usage example:
 * import { UserProfile, ChatMessage, MessageSender, FoodItem, AgentResponse } from './models';
 */

export type { UserProfile } from './user-profile.interface';
export { MessageSender } from './message-sender.enum';
export type { ChatMessage } from './chat-message.interface';
export type { FoodItem } from './food-item.interface';
export type { AgentResponse } from './agent-response.interface';

// Made with Bob
