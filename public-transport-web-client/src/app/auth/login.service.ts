import {Injectable} from '@angular/core';
import {AgencyStorageService} from "./agency-storage.service";
import {GoogleAnalyticsService} from "../google-analytics.service";
import {KeycloakAuthService, KEYCLOAK_LOGIN_RETURN_KEY} from "./keycloak-auth.service";

@Injectable({
    providedIn: 'root'
})
export class LoginService {

    static SESSION_STORAGE_AUTH_TOKEN_KEY = 'token';
    static SESSION_STORAGE_ROLES_KEY = 'roles';

    constructor(private agencyStorageService: AgencyStorageService,
                private googleAnalyticsService: GoogleAnalyticsService,
                private keycloakAuth: KeycloakAuthService) {
    }

    captureKeycloakSession(): void {
        if (!this.keycloakAuth.isLoggedIn()) {
            return;
        }
        const roles = this.keycloakAuth.roles();
        sessionStorage.setItem(LoginService.SESSION_STORAGE_AUTH_TOKEN_KEY, this.keycloakAuth.token());
        sessionStorage.setItem(LoginService.SESSION_STORAGE_ROLES_KEY, JSON.stringify(roles));
        const agencyCode = this.keycloakAuth.agencyCode();
        if (agencyCode) {
            this.agencyStorageService.setInstance(agencyCode);
        }
        if (sessionStorage.getItem(KEYCLOAK_LOGIN_RETURN_KEY) === '1') {
            this.googleAnalyticsService.userLogin(this.keycloakAuth.username());
        }
    }

    loginWithKeycloak(): Promise<void> {
        return this.keycloakAuth.login();
    }

    logout(): void {
        sessionStorage.setItem(LoginService.SESSION_STORAGE_AUTH_TOKEN_KEY, '');
        sessionStorage.setItem(LoginService.SESSION_STORAGE_ROLES_KEY, '[]');
        this.agencyStorageService.setInstance('');
        const redirectUri = window.location.origin + '/#/company';
        this.keycloakAuth.logout(redirectUri);
    }

    isLoggedIn(): boolean {
        return this.keycloakAuth.isLoggedIn();
    }

    getRoles(): string[] {
        return JSON.parse(sessionStorage.getItem(LoginService.SESSION_STORAGE_ROLES_KEY) || '[]');
    }

    getInstance(): string {
        return this.agencyStorageService.getInstance();
    }

    hasRoleSuperAdmin(): boolean {
        return this.getRoles().includes('super_user');
    }

    hasRoleAgencyOwner(): boolean {
        return this.getRoles().includes('agency_owner');
    }

}
