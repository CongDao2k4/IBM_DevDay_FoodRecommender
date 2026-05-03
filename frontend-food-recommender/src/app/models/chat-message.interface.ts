import { MessageSender } from './message-sender.enum';

/**
 * Represents a chat message in the conversation between user and agent
 */
export interface ChatMessage {
  /**
   * The sender of the message (USER or AGENT)
   */
  sender: MessageSender;

  /**
   * The text content of the message
   */
  text: string;

  /**
   * The timestamp when the message was sent
   */
  timestamp: Date;
}

// Made with Bob
