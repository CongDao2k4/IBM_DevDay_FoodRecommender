import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserProfile } from '../models';

/**
 * Component for editing user profile information
 * Displays a form with basic info, medical conditions, and allergens
 */
@Component({
  selector: 'app-user-profile',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.css'
})
export class UserProfileComponent implements OnInit {
  /**
   * Input profile to edit (optional - if not provided, creates new profile)
   */
  @Input() profile?: UserProfile;

  /**
   * Event emitted when the profile is saved
   */
  @Output() profileSaved = new EventEmitter<UserProfile>();

  /**
   * Reactive form for user profile
   */
  profileForm!: FormGroup;

  /**
   * Available medical conditions
   */
  readonly medicalConditionOptions = [
    'Diabetes',
    'High Blood Pressure',
    'Gout'
  ];

  /**
   * Available allergens
   */
  readonly allergenOptions = [
    'Seafood',
    'Peanuts',
    'Soy',
    'Dairy'
  ];

  /**
   * Track form submission state
   */
  isSubmitting = false;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  /**
   * Initialize the reactive form with validation
   */
  private initializeForm(): void {
    // Initialize with mock data if no profile is provided
    const mockProfile = this.profile || {
      id: this.generateId(),
      name: 'John Doe',
      bmi: 26,
      medicalConditions: ['Gout'],
      allergens: ['Seafood']
    };

    this.profileForm = this.fb.group({
      id: [mockProfile.id],
      name: [mockProfile.name, [Validators.required, Validators.minLength(2)]],
      bmi: [
        mockProfile.bmi,
        [Validators.required, Validators.min(10), Validators.max(50)]
      ],
      medicalConditions: this.fb.group(
        this.medicalConditionOptions.reduce((acc, condition) => {
          acc[condition] = [mockProfile.medicalConditions?.includes(condition) || false];
          return acc;
        }, {} as Record<string, boolean[]>)
      ),
      allergens: this.fb.group(
        this.allergenOptions.reduce((acc, allergen) => {
          acc[allergen] = [mockProfile.allergens?.includes(allergen) || false];
          return acc;
        }, {} as Record<string, boolean[]>)
      )
    });
  }

  /**
   * Generate a simple ID for new profiles
   */
  private generateId(): string {
    return `user_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Check if a medical condition is selected
   */
  isMedicalConditionSelected(condition: string): boolean {
    return this.profileForm.get(`medicalConditions.${condition}`)?.value || false;
  }

  /**
   * Check if an allergen is selected
   */
  isAllergenSelected(allergen: string): boolean {
    return this.profileForm.get(`allergens.${allergen}`)?.value || false;
  }

  /**
   * Get selected medical conditions from form
   */
  private getSelectedMedicalConditions(): string[] {
    const medicalConditionsGroup = this.profileForm.get('medicalConditions');
    if (!medicalConditionsGroup) return [];

    return this.medicalConditionOptions.filter(
      condition => medicalConditionsGroup.get(condition)?.value === true
    );
  }

  /**
   * Get selected allergens from form
   */
  private getSelectedAllergens(): string[] {
    const allergensGroup = this.profileForm.get('allergens');
    if (!allergensGroup) return [];

    return this.allergenOptions.filter(
      allergen => allergensGroup.get(allergen)?.value === true
    );
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {
    if (this.profileForm.invalid) {
      // Mark all fields as touched to show validation errors
      Object.keys(this.profileForm.controls).forEach(key => {
        this.profileForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isSubmitting = true;

    const formValue = this.profileForm.value;
    const updatedProfile: UserProfile = {
      id: formValue.id,
      name: formValue.name,
      bmi: formValue.bmi,
      medicalConditions: this.getSelectedMedicalConditions(),
      allergens: this.getSelectedAllergens()
    };

    // Emit the updated profile
    this.profileSaved.emit(updatedProfile);

    // Reset submitting state after a short delay
    setTimeout(() => {
      this.isSubmitting = false;
    }, 500);
  }

  /**
   * Check if a form field has an error and has been touched
   */
  hasError(fieldName: string, errorType: string): boolean {
    const field = this.profileForm.get(fieldName);
    return !!(field?.hasError(errorType) && field?.touched);
  }

  /**
   * Get error message for a field
   */
  getErrorMessage(fieldName: string): string {
    const field = this.profileForm.get(fieldName);
    if (!field || !field.touched) return '';

    if (field.hasError('required')) {
      return `${fieldName} is required`;
    }
    if (field.hasError('minlength')) {
      return `${fieldName} must be at least ${field.errors?.['minlength'].requiredLength} characters`;
    }
    if (field.hasError('min')) {
      return `${fieldName} must be at least ${field.errors?.['min'].min}`;
    }
    if (field.hasError('max')) {
      return `${fieldName} must be at most ${field.errors?.['max'].max}`;
    }
    return '';
  }
}

// Made with Bob
