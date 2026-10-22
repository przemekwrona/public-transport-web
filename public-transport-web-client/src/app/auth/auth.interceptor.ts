import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandler, HttpHeaders,
    HttpInterceptor,
    HttpInterceptorFn,
    HttpRequest, HttpResponse
} from '@angular/common/http';
import {Injectable} from "@angular/core";
import {catchError, Observable, of, tap, throwError} from "rxjs";
import {LoginService} from "./login.service";
import {AgencyStorageService} from "./agency-storage.service";
import {Router} from "@angular/router";

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    return next(req);
};

@Injectable()
export class AddHeaderInterceptor implements HttpInterceptor {

    constructor(private _router: Router) {
    }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // Get token
        const token: string = sessionStorage.getItem(LoginService.SESSION_STORAGE_AUTH_TOKEN_KEY) || '';
        const isLogin = req.url.includes('/auth/login');
        const clonedRequest = !isLogin && token
            ? req.clone({headers: req.headers.set('Authorization', `Bearer ${token}`)})
            : req;

        // Pass the cloned request instead of the original request to the next handle
        return next.handle(clonedRequest)
            .pipe(tap((event: HttpEvent<any>) => {
                if (event instanceof HttpResponse) {
                    const headers: HttpHeaders = event.headers;
                }
            }))
            .pipe(catchError((error: HttpErrorResponse) => {
                const httpStatus: number = error.status;
                if (httpStatus === 401) {
                    sessionStorage.setItem(LoginService.SESSION_STORAGE_AUTH_TOKEN_KEY, '');
                    sessionStorage.setItem(LoginService.SESSION_STORAGE_ROLES_KEY, '[]');
                    sessionStorage.setItem(AgencyStorageService.SESSION_STORAGE_INSTANCE_KEY, '');
                    if (!isLogin) {
                        this._router.navigate(['/signin']);
                    }
                    return of();
                } else {
                    return throwError(() => error);
                }
            }));
    }
}
