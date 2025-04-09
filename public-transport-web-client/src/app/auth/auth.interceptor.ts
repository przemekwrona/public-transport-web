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
import {AuthService} from "./auth.service";
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
        const token: string = sessionStorage.getItem(AuthService.SESSION_STORAGE_AUTH_TOKEN_KEY) || '';

        // Clone the request to add the new header
        const clonedRequest = req.clone({headers: req.headers.append('Authorization', `Bearer ${token}`)});

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
                    this._router.navigate(['/signin']);
                    return of();
                } else {
                    return throwError(() => error);
                }
            }));
    }
}
