import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserProfileComponent } from './user-profile.component';
import { AgentWorkspaceComponent } from './agent-workspace.component';
import { UserProfile } from '../models';
import { NutritionAgentService } from '../services/nutrition-agent.service';

/**
 * Main dashboard component that integrates the user profile and agent workspace
 * Provides the overall layout and coordinates data flow between components
 */
@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, UserProfileComponent, AgentWorkspaceComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  /**
   * Inject the nutrition agent service
   */
  private nutritionService = inject(NutritionAgentService);

  /**
   * Current user profile
   */
  currentProfile?: UserProfile;

  /**
   * Current user ID for the workspace
   */
  currentUserId = 'user_default';

  /**
   * Loading state for profile
   */
  isLoadingProfile = false;

  /**
   * Success message for profile save
   */
  successMessage = '';

  /**
   * Error message for profile operations
   */
  errorMessage = '';

  /**
   * Controls visibility of the profile slide-out panel
   */
  isProfileVisible = false;

  ngOnInit(): void {
    // Initialize with a default user ID
    this.currentUserId = this.generateDefaultUserId();
    
    // Optionally load existing profile
    // this.loadUserProfile(this.currentUserId);
  }

  /**
   * Generate a default user ID
   */
  private generateDefaultUserId(): string {
    return `user_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Load user profile from the service
   */
  loadUserProfile(userId: string): void {
    this.isLoadingProfile = true;
    this.errorMessage = '';

    this.nutritionService.getUserProfile(userId).subscribe({
      next: (profile) => {
        this.currentProfile = profile;
        this.currentUserId = profile.id;
        this.isLoadingProfile = false;
      },
      error: (error) => {
        this.errorMessage = `Failed to load profile: ${error.message}`;
        this.isLoadingProfile = false;
      }
    });
  }

  /**
   * Handle profile saved event from UserProfileComponent
   */
  onProfileSaved(profile: UserProfile): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.isLoadingProfile = true;

    // Update profile via service
    this.nutritionService.updateUserProfile(profile).subscribe({
      next: (updatedProfile) => {
        this.currentProfile = updatedProfile;
        this.currentUserId = updatedProfile.id;
        this.successMessage = 'Profile saved successfully!';
        this.isLoadingProfile = false;
        
        // Hide the profile panel after successful save
        this.isProfileVisible = false;

        // Clear success message after 3 seconds
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      },
      error: (error) => {
        this.errorMessage = `Failed to save profile: ${error.message}`;
        this.isLoadingProfile = false;

        // Clear error message after 5 seconds
        setTimeout(() => {
          this.errorMessage = '';
        }, 5000);
      }
    });
  }

  /**
   * Toggle profile panel visibility
   */
  toggleProfile(): void {
    this.isProfileVisible = !this.isProfileVisible;
  }

  /**
   * Clear all messages
   */
  clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}

// Made with Bob
