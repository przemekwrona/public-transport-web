import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BusStopModalSelectorComponent } from './bus-stop-modal-selector.component';

describe('BusStopModalSelectorComponent', () => {
  let component: BusStopModalSelectorComponent;
  let fixture: ComponentFixture<BusStopModalSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BusStopModalSelectorComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(BusStopModalSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
