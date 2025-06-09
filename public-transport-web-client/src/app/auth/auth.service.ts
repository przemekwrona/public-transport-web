import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, tap} from "rxjs";
import {LoginAppUserRequest, LoginAppUserResponse} from "../generated/public-transport";

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    static SESSION_STORAGE_AUTH_TOKEN_KEY = 'token';

    private roles: string[] = [];

    constructor(private httpClient: HttpClient) {
    }

    login(loginCredentials: LoginAppUserRequest): Observable<LoginAppUserResponse> {
        return this.httpClient.post<LoginAppUserResponse>(`/api/v1/auth/login`, loginCredentials)
            .pipe(tap((response: LoginAppUserResponse) => {
                sessionStorage.setItem(AuthService.SESSION_STORAGE_AUTH_TOKEN_KEY, response.token || '');
                this.roles = response.roles;
            }));
    }

    logout(): void {
        sessionStorage.setItem(AuthService.SESSION_STORAGE_AUTH_TOKEN_KEY, '');
        this.roles = [];
    }

    hasRoleSuperAdmin(): boolean {
        return this.roles.includes('SUPER_USER');
    }

}
