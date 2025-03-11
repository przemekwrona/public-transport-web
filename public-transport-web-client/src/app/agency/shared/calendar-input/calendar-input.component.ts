import {Component, forwardRef} from '@angular/core';
import moment from "moment";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";

@Component({
    selector: 'app-calendar-input',
    templateUrl: './calendar-input.component.html',
    styleUrl: './calendar-input.component.scss',
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => CalendarInputComponent),
            multi: true
        }
    ]
})
export class CalendarInputComponent implements ControlValueAccessor {

    onChange = (value: string) => {};
    onTouched = () => {};

    public isFocus: boolean = false;

    public selectedDate: string = moment().format('YYYY-MM-DD');

    public includeDays: Set<string> = new Set<string>;

    public onChangeIncludeDays($event: Set<string>) {
        this.includeDays = $event;
        this.selectedDate = [...$event][0];
        this.onChange(this.selectedDate)
    }

    registerOnChange(fn: (value: string) => void): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    writeValue(value: string): void {
        this.selectedDate = value;
        this.includeDays = new Set<string>();
        this.includeDays.add(this.selectedDate);
    }

    onInput(event: Event): void {
        const inputValue = (event.target as HTMLInputElement).value;
        this.selectedDate = inputValue;
        this.onChange(inputValue);
    }
}
