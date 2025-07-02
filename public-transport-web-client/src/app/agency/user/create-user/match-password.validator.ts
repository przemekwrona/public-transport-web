import {AbstractControl, ValidatorFn} from "@angular/forms";

export function matchPasswordValidator(passwordKey: string, confirmKey: string): ValidatorFn {

    return (group: AbstractControl): { [key: string]: any } | null => {
        const password = group.get(passwordKey)?.value;
        const confirm = group.get(confirmKey)?.value;

        if (password !== confirm) {
            return { passwordMismatch: true };
        }

        return null;
    }

}
