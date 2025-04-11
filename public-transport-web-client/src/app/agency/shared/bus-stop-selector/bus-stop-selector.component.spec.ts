import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BusStopSelectorComponent } from './bus-stop-selector.component';

describe('BusStopSelectorComponent', () => {
  let component: BusStopSelectorComponent;
  let fixture: ComponentFixture<BusStopSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BusStopSelectorComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(BusStopSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
