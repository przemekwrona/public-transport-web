import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, tap} from "rxjs";
import {LoginAppUserRequest, LoginAppUserResponse} from "../generated/public-transport";

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    static SESSION_STORAGE_AUTH_TOKEN_KEY = 'token';
    static SESSION_STORAGE_ROLES_KEY = 'roles';
    static SESSION_STORAGE_INSTANCE_KEY = 'instance';

    constructor(private httpClient: HttpClient) {
    }

    login(loginCredentials: LoginAppUserRequest): Observable<LoginAppUserResponse> {
        return this.httpClient.post<LoginAppUserResponse>(`/api/v1/auth/login`, loginCredentials)
            .pipe(tap((response: LoginAppUserResponse) => {
                sessionStorage.setItem(AuthService.SESSION_STORAGE_AUTH_TOKEN_KEY, response.token || '');
                sessionStorage.setItem(AuthService.SESSION_STORAGE_ROLES_KEY, JSON.stringify(response.roles) || '[]');
                sessionStorage.setItem(AuthService.SESSION_STORAGE_INSTANCE_KEY, response.instance || '');
            }));
    }

    logout(): void {
        sessionStorage.setItem(AuthService.SESSION_STORAGE_AUTH_TOKEN_KEY, '');
        sessionStorage.setItem(AuthService.SESSION_STORAGE_ROLES_KEY, '[]');
        sessionStorage.setItem(AuthService.SESSION_STORAGE_INSTANCE_KEY, '');
    }

    getRoles(): string[] {
        return JSON.parse(sessionStorage.getItem(AuthService.SESSION_STORAGE_ROLES_KEY)) || [];
    }

    getInstance(): string {
        return sessionStorage.getItem(AuthService.SESSION_STORAGE_INSTANCE_KEY) || '';
    }

    hasRoleSuperAdmin(): boolean {
        return this.getRoles().includes('SUPER_USER');
    }

    hasRoleAgencyOwner(): boolean {
        return this.getRoles().includes('AGENCY_OWNER');
    }

}
