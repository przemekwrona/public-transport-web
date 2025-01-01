import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-field',
  templateUrl: './field.component.html',
  styleUrl: './field.component.scss'
})
export class FieldComponent {

  @Input() public key: string = '';
  @Input() public value: string | number = '';

}
