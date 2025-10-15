import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, tap} from "rxjs";
import {AuthService, LoginAppUserRequest, LoginAppUserResponse} from "../generated/public-transport-api";

@Injectable({
    providedIn: 'root'
})
export class LoginService {

    static SESSION_STORAGE_AUTH_TOKEN_KEY = 'token';
    static SESSION_STORAGE_ROLES_KEY = 'roles';
    static SESSION_STORAGE_INSTANCE_KEY = 'instance';

    constructor(private httpClient: HttpClient, private authService: AuthService) {
    }

    login(loginCredentials: LoginAppUserRequest): Observable<LoginAppUserResponse> {
        return this.authService.login(loginCredentials)
            .pipe(tap((response: LoginAppUserResponse) => {
                sessionStorage.setItem(LoginService.SESSION_STORAGE_AUTH_TOKEN_KEY, response.token || '');
                sessionStorage.setItem(LoginService.SESSION_STORAGE_ROLES_KEY, JSON.stringify(response.roles) || '[]');
                sessionStorage.setItem(LoginService.SESSION_STORAGE_INSTANCE_KEY, response.instance || '');
            }));
    }

    logout(): void {
        sessionStorage.setItem(LoginService.SESSION_STORAGE_AUTH_TOKEN_KEY, '');
        sessionStorage.setItem(LoginService.SESSION_STORAGE_ROLES_KEY, '[]');
        sessionStorage.setItem(LoginService.SESSION_STORAGE_INSTANCE_KEY, '');
    }

    getRoles(): string[] {
        return JSON.parse(sessionStorage.getItem(LoginService.SESSION_STORAGE_ROLES_KEY)) || [];
    }

    getInstance(): string {
        return sessionStorage.getItem(LoginService.SESSION_STORAGE_INSTANCE_KEY) || '';
    }

    hasRoleSuperAdmin(): boolean {
        return this.getRoles().includes('SUPER_USER');
    }

    hasRoleAgencyOwner(): boolean {
        return this.getRoles().includes('AGENCY_OWNER');
    }

    setInstance(instance: string): void {
        sessionStorage.setItem(LoginService.SESSION_STORAGE_INSTANCE_KEY, instance || '');
    }

}
