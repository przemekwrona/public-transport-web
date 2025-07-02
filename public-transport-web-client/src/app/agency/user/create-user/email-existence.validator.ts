import {AbstractControl, ValidatorFn} from "@angular/forms";

export function emailExistenceValidator(): ValidatorFn {

    return (control: AbstractControl): { [key: string]: any } | null => {
        const value = control.value;
        // return {exists: true};
        return null;
    }

}
