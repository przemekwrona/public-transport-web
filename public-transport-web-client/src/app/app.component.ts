import {AfterViewInit, Component, HostBinding, OnInit} from '@angular/core';
import KTComponents from "../metronic/core";
import KTLayout from '../metronic/app/layouts/demo1';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrl: './app.component.css',
    standalone: false
})
export class AppComponent implements OnInit, AfterViewInit {
  title = 'public-transport-web-client';
  // @HostBinding('class') hostClass = 'flex grow';

  ngAfterViewInit(): void {
    // KTComponents.init();
    // KTLayout.init();
  }

  ngOnInit(): void {
  }
}
