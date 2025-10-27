import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, tap} from "rxjs";
import {AuthService, LoginAppUserRequest, LoginAppUserResponse} from "../generated/public-transport-api";
import {AgencyStorageService} from "./agency-storage.service";
import {GoogleAnalyticsService} from "../google-analytics.service";

@Injectable({
    providedIn: 'root'
})
export class LoginService {

    static SESSION_STORAGE_AUTH_TOKEN_KEY = 'token';
    static SESSION_STORAGE_ROLES_KEY = 'roles';

    constructor(private httpClient: HttpClient, private authService: AuthService, private agencyStorageService: AgencyStorageService, private googleAnalyticsService: GoogleAnalyticsService) {
    }

    login(loginCredentials: LoginAppUserRequest): Observable<LoginAppUserResponse> {
        return this.authService.login(loginCredentials)
            .pipe(tap((response: LoginAppUserResponse) => {
                sessionStorage.setItem(LoginService.SESSION_STORAGE_AUTH_TOKEN_KEY, response.token || '');
                sessionStorage.setItem(LoginService.SESSION_STORAGE_ROLES_KEY, JSON.stringify(response.roles) || '[]');
                this.agencyStorageService.setInstance(response.instance);
                this.googleAnalyticsService.userLogin(loginCredentials.username);
            }));
    }

    logout(): void {
        sessionStorage.setItem(LoginService.SESSION_STORAGE_AUTH_TOKEN_KEY, '');
        sessionStorage.setItem(LoginService.SESSION_STORAGE_ROLES_KEY, '[]');
        this.agencyStorageService.setInstance('');
    }

    getRoles(): string[] {
        return JSON.parse(sessionStorage.getItem(LoginService.SESSION_STORAGE_ROLES_KEY)) || [];
    }

    getInstance(): string {
        return this.agencyStorageService.getInstance();
    }

    hasRoleSuperAdmin(): boolean {
        return this.getRoles().includes('SUPER_USER');
    }

    hasRoleAgencyOwner(): boolean {
        return this.getRoles().includes('AGENCY_OWNER');
    }

}
