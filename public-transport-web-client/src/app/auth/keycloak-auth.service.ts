import {Injectable} from '@angular/core';
import Keycloak from 'keycloak-js';
import {environment} from '../../environments/environment';

export const KEYCLOAK_LOGIN_RETURN_KEY = 'kc-login-return';

@Injectable({
    providedIn: 'root'
})
export class KeycloakAuthService {

    private keycloak: Keycloak | null = null;

    init(): Promise<boolean> {
        this.keycloak = new Keycloak({
            url: environment.keycloak.url,
            realm: environment.keycloak.realm,
            clientId: environment.keycloak.clientId
        });
        return this.keycloak.init({
            onLoad: 'check-sso',
            pkceMethod: 'S256',
            checkLoginIframe: false,
            silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html'
        }).catch((error: unknown) => {
            console.error('Keycloak init failed', error);
            return false;
        });
    }

    isLoggedIn(): boolean {
        return this.keycloak?.authenticated === true;
    }

    token(): string {
        return this.keycloak?.token || '';
    }

    username(): string {
        return this.tokenParsed().preferred_username || '';
    }

    roles(): string[] {
        const parsed = this.tokenParsed();
        const realmRoles = parsed.realm_access?.roles || [];
        const clientRoles = parsed.resource_access?.[environment.keycloak.clientId]?.roles || [];
        return [...realmRoles, ...clientRoles];
    }

    agencyCode(): string {
        const groups = this.tokenParsed().groups || [];
        for (const group of groups) {
            const match = /^\/agencies\/([^/]+)\//.exec(group);
            if (match) {
                return match[1];
            }
        }
        return '';
    }

    login(): Promise<void> {
        sessionStorage.setItem(KEYCLOAK_LOGIN_RETURN_KEY, '1');
        if (!this.keycloak) {
            return Promise.reject(new Error('Keycloak is not initialized'));
        }
        return this.keycloak.login({
            redirectUri: window.location.origin + '/'
        });
    }

    logout(redirectUri: string): Promise<void> {
        if (!this.keycloak) {
            return Promise.resolve();
        }
        return this.keycloak.logout({redirectUri});
    }

    updateToken(minValiditySeconds: number): Promise<string> {
        if (!this.keycloak?.authenticated) {
            return Promise.resolve('');
        }
        return this.keycloak.updateToken(minValiditySeconds).then(() => this.keycloak?.token || '');
    }

    private tokenParsed(): KeycloakProfileToken {
        return (this.keycloak?.tokenParsed || {}) as KeycloakProfileToken;
    }
}

interface KeycloakProfileToken {
    preferred_username?: string;
    realm_access?: { roles?: string[] };
    resource_access?: { [clientId: string]: { roles?: string[] } };
    groups?: string[];
}
