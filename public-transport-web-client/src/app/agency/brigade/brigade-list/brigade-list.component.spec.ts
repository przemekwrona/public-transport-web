import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BrigadeListComponent } from './brigade-list.component';

describe('BrigadeListComponent', () => {
  let component: BrigadeListComponent;
  let fixture: ComponentFixture<BrigadeListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BrigadeListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(BrigadeListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
