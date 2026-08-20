import {AbstractControl, FormArray, FormGroup} from "@angular/forms";

export interface AllValidationErrors {
    controlName: string;
    errorName: string;
    errorValue: any;
}

export class FormUtils {

    static getFormValidationErrors(control: AbstractControl, errors: AllValidationErrors[] = [], path: string = ''): AllValidationErrors[] {
        if (control.errors) {
            Object.entries(control.errors).forEach(([errorName, errorValue]) => {
                errors.push({
                    controlName: path || 'form',
                    errorName,
                    errorValue
                });
            });
        }

        if (control instanceof FormArray) {
            control.controls.forEach((childControl, index) => {
                const childPath = `${path}[${index}]`;
                FormUtils.getFormValidationErrors(childControl, errors, childPath);
            });
            return errors;
        }

        if (control instanceof FormGroup) {
            Object.entries(control.controls).forEach(([key, childControl]) => {
                const childPath = path ? `${path}.${key}` : key;
                FormUtils.getFormValidationErrors(childControl, errors, childPath);
            });
            return errors;
        }

        return errors;
    }

}