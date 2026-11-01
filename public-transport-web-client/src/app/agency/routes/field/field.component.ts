import {Component, Input, ChangeDetectionStrategy} from '@angular/core';

@Component({
    selector: 'app-field',
    templateUrl: './field.component.html',
    styleUrl: './field.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export class FieldComponent {

  @Input() public key: string = '';
  @Input() public value: string | number = '';

}
