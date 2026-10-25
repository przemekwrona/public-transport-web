import {Injectable} from '@angular/core';
import {AsyncValidatorFn, FormGroup} from '@angular/forms';
import {Observable, of} from 'rxjs';
import {TripMode} from "../../../../generated/public-transport-api";

@Injectable({providedIn: 'root'})
export class TripIdExistenceValidator {

    variantExistsValidator(_line: string, _name: string, _variantName: string, _tripMode: TripMode): AsyncValidatorFn {
        return (_control: FormGroup): Observable<null> => of(null);
    }
}
