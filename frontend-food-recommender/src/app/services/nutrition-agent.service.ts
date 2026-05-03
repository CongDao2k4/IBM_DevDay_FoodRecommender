import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { UserProfile, AgentResponse } from '../models';

/**
 * Service for interacting with the Nutrition Agent backend API
 * Handles user profiles and AI-powered food recommendations
 */
@Injectable({
  providedIn: 'root'
})
export class NutritionAgentService {
  /**
   * Base URL for the backend API
   * TODO: Move to environment configuration
   */
  private readonly apiUrl = 'http://localhost:8080/api';

  /**
   * HTTP headers for JSON requests
   */
  private readonly httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  };

  constructor(private http: HttpClient) {}

  /**
   * Retrieves a user's profile by their ID
   * @param userId - The unique identifier of the user
   * @returns Observable containing the user's profile
   */
  getUserProfile(userId: string): Observable<UserProfile> {
    const url = `${this.apiUrl}/users/${userId}`;
    
    return this.http.get<UserProfile>(url).pipe(
      retry(2), // Retry failed requests up to 2 times
      catchError(this.handleError)
    );
  }

  /**
   * Updates an existing user's profile
   * @param profile - The user profile to update (must include id)
   * @returns Observable containing the updated user profile
   */
  updateUserProfile(profile: UserProfile): Observable<UserProfile> {
    const url = `${this.apiUrl}/users/${profile.id}`;
    
    return this.http.put<UserProfile>(url, profile, this.httpOptions).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Sends a message to the AI agent and receives personalized food recommendations
   * @param userId - The unique identifier of the user
   * @param message - The user's message/query to the AI agent
   * @returns Observable containing the agent's response and food recommendations
   */
  sendMessageToAgent(userId: string, message: string): Observable<AgentResponse> {
    const url = `${this.apiUrl}/agent/chat`;
    
    const payload = {
      userId,
      message
    };
    
    return this.http.post<AgentResponse>(url, payload, this.httpOptions).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Handles HTTP errors and provides user-friendly error messages
   * @param error - The HTTP error response
   * @returns Observable that throws a formatted error message
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unknown error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = `Network error: ${error.error.message}`;
    } else {
      // Backend returned an unsuccessful response code
      switch (error.status) {
        case 400:
          errorMessage = 'Bad request. Please check your input.';
          break;
        case 401:
          errorMessage = 'Unauthorized. Please log in.';
          break;
        case 403:
          errorMessage = 'Access forbidden.';
          break;
        case 404:
          errorMessage = 'Resource not found.';
          break;
        case 500:
          errorMessage = 'Internal server error. Please try again later.';
          break;
        case 503:
          errorMessage = 'Service unavailable. Please try again later.';
          break;
        default:
          errorMessage = `Server error: ${error.status} - ${error.message}`;
      }

      // Include backend error message if available
      if (error.error?.message) {
        errorMessage += ` Details: ${error.error.message}`;
      }
    }

    console.error('HTTP Error:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
}

// Made with Bob
