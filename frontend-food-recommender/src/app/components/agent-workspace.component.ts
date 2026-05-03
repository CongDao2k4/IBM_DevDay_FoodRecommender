import { Component, ElementRef, Input, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatMessage, FoodItem, MessageSender } from '../models';
import { NutritionAgentService } from '../services/nutrition-agent.service';

/**
 * Main workspace component for interacting with the AI nutrition agent
 * Features a split-panel layout with chat interface and food recommendations
 */
@Component({
  selector: 'app-agent-workspace',
  imports: [CommonModule, FormsModule],
  templateUrl: './agent-workspace.component.html',
  styleUrl: './agent-workspace.component.css'
})
export class AgentWorkspaceComponent implements OnInit {
  /**
   * User ID for the current session
   */
  @Input() userId: string = 'user_default';

  /**
   * Reference to the chat messages container for auto-scrolling
   */
  @ViewChild('chatContainer') private chatContainer?: ElementRef;

  /**
   * Inject the nutrition agent service
   */
  private nutritionService = inject(NutritionAgentService);

  /**
   * Array of chat messages
   */
  messages: ChatMessage[] = [];

  /**
   * Array of food recommendations
   */
  recommendations: FoodItem[] = [];

  /**
   * Current message being typed by the user
   */
  currentMessage = '';

  /**
   * Loading state for sending messages
   */
  isSending = false;

  /**
   * Error message to display
   */
  errorMessage = '';

  /**
   * Expose MessageSender enum to template
   */
  readonly MessageSender = MessageSender;

  ngOnInit(): void {
    // Add personalized welcome message from agent with mock data
    this.addAgentMessage(
      'Hello! I see you are managing Gout and a Seafood allergy. What would you like to eat today?'
    );

    // Initialize with mock food recommendations
    this.recommendations = [
      {
        id: 'food_1',
        name: 'Organic Spinach',
        category: 'Vegetables',
        warningIndicator: false,
        reason: 'Excellent choice! Spinach is low in purines and rich in vitamins and minerals. Perfect for managing Gout.'
      },
      {
        id: 'food_2',
        name: 'Shrimp',
        category: 'Seafood',
        warningIndicator: true,
        reason: 'Contains seafood, which triggers your allergy and is high in purines. This could worsen your Gout symptoms.'
      }
    ];
  }

  /**
   * Send a message to the AI agent
   */
  async sendMessage(): Promise<void> {
    if (!this.currentMessage.trim() || this.isSending) {
      return;
    }

    const userMessage = this.currentMessage.trim();
    this.currentMessage = '';
    this.errorMessage = '';
    this.isSending = true;

    // Add user message to chat
    this.addUserMessage(userMessage);

    try {
      // Call the service to send message to agent
      this.nutritionService.sendMessageToAgent(this.userId, userMessage).subscribe({
        next: (response) => {
          // Add agent response to chat
          this.addAgentMessage(response.response);
          
          // Update recommendations
          this.recommendations = response.recommendations;
          
          this.isSending = false;
        },
        error: (error) => {
          this.errorMessage = error.message || 'Failed to send message. Please try again.';
          this.addAgentMessage(
            'I apologize, but I encountered an error processing your request. Please try again.'
          );
          this.isSending = false;
        }
      });
    } catch (error) {
      this.errorMessage = 'An unexpected error occurred. Please try again.';
      this.isSending = false;
    }
  }

  /**
   * Add a user message to the chat
   */
  private addUserMessage(text: string): void {
    const message: ChatMessage = {
      sender: MessageSender.USER,
      text,
      timestamp: new Date()
    };
    this.messages.push(message);
    this.scrollToBottom();
  }

  /**
   * Add an agent message to the chat
   */
  private addAgentMessage(text: string): void {
    const message: ChatMessage = {
      sender: MessageSender.AGENT,
      text,
      timestamp: new Date()
    };
    this.messages.push(message);
    this.scrollToBottom();
  }

  /**
   * Scroll chat to bottom
   */
  private scrollToBottom(): void {
    setTimeout(() => {
      if (this.chatContainer) {
        const element = this.chatContainer.nativeElement;
        element.scrollTop = element.scrollHeight;
      }
    }, 100);
  }

  /**
   * Handle Enter key press in message input
   */
  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  /**
   * Format timestamp for display
   */
  formatTime(date: Date): string {
    return new Date(date).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Check if message is from user
   */
  isUserMessage(message: ChatMessage): boolean {
    return message.sender === MessageSender.USER;
  }

  /**
   * Check if message is from agent
   */
  isAgentMessage(message: ChatMessage): boolean {
    return message.sender === MessageSender.AGENT;
  }

  /**
   * Get warning count from recommendations
   */
  getWarningCount(): number {
    return this.recommendations.filter(item => item.warningIndicator).length;
  }

  /**
   * Get safe items count from recommendations
   */
  getSafeCount(): number {
    return this.recommendations.filter(item => !item.warningIndicator).length;
  }

  /**
   * Clear all messages and recommendations
   */
  clearWorkspace(): void {
    this.messages = [];
    this.recommendations = [];
    this.errorMessage = '';
    this.addAgentMessage(
      'Workspace cleared. How can I help you with your nutrition today?'
    );
  }
}

// Made with Bob
