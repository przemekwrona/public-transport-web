import {ResolveFn} from '@angular/router';
import {CenterPoint, StopsService} from "../../generated/public-transport-api";
import {inject} from "@angular/core";
import {Observable} from "rxjs";

export const centerPointResolver: ResolveFn<CenterPoint> = (route, state): Observable<CenterPoint> => {
    const stopService: StopsService = inject(StopsService);

    return stopService.centerMap();
};
