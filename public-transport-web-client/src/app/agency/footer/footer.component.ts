import {Component, HostBinding, OnInit} from '@angular/core';
import moment from "moment";

@Component({
    selector: 'app-footer',
    templateUrl: './footer.component.html',
    styleUrl: './footer.component.scss',
    standalone: false
})
export class FooterComponent implements OnInit {
  @HostBinding('class') hostClass = 'footer';

  public now: moment.Moment;

  ngOnInit(): void {
    this.now = moment();
  }
}
